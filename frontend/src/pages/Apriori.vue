<template>
  <q-page padding>
    <div class="q-gutter-md row">
      <div class="q-gutter-md">
        <q-input
          filled
          v-model="databaseSelectModel"
          label="Database"
          disable
          style="width: 250px; padding-bottom: 32px"
        />

        <q-select
          filled
          :model-value="collectionSelectModel"
          use-input
          hide-selected
          fill-input
          input-debounce="0"
          :options="collectionSelectValues"
          @filter="filterCollection"
          @input-value="setCollectionSelect"
          style="width: 250px; padding-bottom: 32px"
          label="Select collection *"
          :rules="[(val) => !!val || 'Select a collection']"
        />

        <q-select
          filled
          v-model="fieldSelectModel"
          multiple
          input-debounce="0"
          :options="fieldSelectValues"
          style="width: 250px; padding-bottom: 32px"
          label="Select fields *"
          :disable="!databaseSelectModel && !collectionSelectModel"
          :rules="[(val) => !!val || 'Select a field or fields']"
        />

        <q-input
          filled
          type="number"
          v-model="minimumSupport"
          label="Minimum support *"
          style="width: 250px; padding-bottom: 32px"
          mask="#.##"
          :decimals="2"
          :rules="[
            (val) => val >= 0 || 'Positive number',
            (val) => val >= 0.01 && val <= 1|| 'Number must be between 0 and 1',
            (val) => (val && val.length > 0)  || 'One number at least',
          ]"
        />

        <q-input
          filled
          type="number"
          v-model="minimumConfidence"
          label="Minimum confidence *"
          style="width: 250px; padding-bottom: 32px"
          :decimals="2"
          mask="#.##"
          :rules="[
            (val) => val >= 0 || 'Positive number',
            (val) => val >= 0.01 && val <= 1|| 'Number must be between 0 and 1',
            (val) => (val && val.length > 0) || 'One number at least',
          ]"
        />

        <q-input
          filled
          type="number"
          v-model="itemCount"
          label="Item count *"
          lazy-rules
          style="width: 250px; padding-bottom: 32px"
          :rules="[
            (val) => val > 1 || 'Item count needs to be positive and higher than 1',
            (val) => (val && val.length > 0) || 'One number at least',
          ]"
        />

        <div>
          <q-btn
            label="Start"
            type="submit"
            color="primary"
            @click="onSubmit"
          />
        </div>
      </div>

      <div class="q-pa-md" style="min-width: 80%">
        <q-input
          id="textArea"
          v-model="textAreaText"
          filled
          type="textarea"
          rows="40"
          readonly
        />
      </div>
    </div>
  </q-page>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted, reactive } from 'vue';
import { Database } from 'src/models/database-models';
import { AprioriProperties } from 'src/models/algorithm-models';
import DataService from 'src/services/data-service';
import AlgorithmService from 'src/services/algorithm-service';

const fieldsStore: Record<string, Array<string>> = {};

const fillFieldsStore = async (
  database: string,
  collection: string
): Promise<string[]> => {
  const key = `${database}.${collection}`;

  if (!fieldsStore[key]) {
    try {
      const fieldsData = await DataService.getFields(database, collection);
      fieldsStore[key] = fieldsData.data.fields;
    } catch (e) {
      console.error('Something went wrong fetching data from /fields');
    }
  }

  return fieldsStore[key];
};

export default defineComponent({
  name: 'Algorithm',
  setup() {
    const databaseSelectValues = reactive<string[]>([]);
    const databaseSelectModel = ref<string>();

    const collectionSelectModel = ref<string>();
    const collectionSelectValues = reactive<string[]>([]);
    const collectionValues = reactive<string[]>([]);

    const fieldSelectModel = ref<string[]>([]);
    const fieldSelectValues = reactive<string[]>(['temp', 'temp2']);
    const fieldValues = reactive<string[]>([]);

    const minimumSupport = ref<number>();
    const minimumConfidence = ref<number>();
    const itemCount = ref<number>();

    const fetchedData: Database[] = [];

    const textAreaText = ref<string>('');

    const textArea = ref<Element | null>();

    //TODO: array for fields
    const fetchApriori = (properties: AprioriProperties) => {
      try {
        const createdEventSource = AlgorithmService.apriori(properties);
        createdEventSource.onmessage = (event: MessageEvent) => {
          const data = event.data.replaceAll(';', '\n\t');

          //TODO: add close statement
          if (data.includes('It took')) createdEventSource.close();
          textAreaText.value =
            data === ''
              ? `${textAreaText.value}${data}`
              : `${textAreaText.value} ${data} \n`;

          if (textArea.value != null)
            textArea.value.scrollTop =
              textArea.value.scrollHeight - textArea.value.clientHeight;
        };
      } catch (e) {
        console.error('Something went wrong fetching data from /apriori');
      }
    };

    onMounted(async () => {
      try {
        const data = await DataService.getDatabases();

        fetchedData.push(...data.data);
        data.data.map((value: Database) =>
          value.collections.map((collection: string) =>
            collectionValues.push(collection)
          )
        );

        collectionSelectValues.push(...collectionValues);

        textArea.value = document.querySelector('.q-textarea textarea');
      } catch (error) {
        console.error(error);
      }
    });

    return {
      filterCollection(val: string, update: (callback: () => void) => void) {
        update(() => {
          const needle = val.toLowerCase();
          collectionSelectValues.length = 0;
          collectionSelectValues.push(
            ...collectionValues.filter(
              (v) => v.toLowerCase().indexOf(needle) > -1
            )
          );
        });
      },
      async setCollectionSelect(val: string) {
        collectionSelectModel.value = val;

        let foundDatabase: string | undefined = undefined;

        fetchedData.every((database: Database) => {
          const collection = database.collections.find(
            (collectionName: string) => collectionName === val
          );

          if (collection) {
            foundDatabase = database.databaseName;
            return false;
          }
          return true;
        });
        if (!foundDatabase)
          throw new Error(`Could not find database ${foundDatabase}`);
        databaseSelectModel.value = foundDatabase;

        const fields = await fillFieldsStore(
          databaseSelectModel.value,
          collectionSelectModel.value
        );
        fieldSelectValues.length = 0;
        fieldSelectValues.push(...fields);

        fieldValues.length = 0;
        fieldValues.push(...fields);

        fieldSelectModel.value.length = 0;
      },
      onSubmit() {
        textAreaText.value = '';
        if (!databaseSelectModel.value) throw new Error('databaseSelectModel was empty');
        if (!collectionSelectModel.value) throw new Error('collectionSelectModel was empty');
        if (!fieldSelectModel.value) throw new Error('fieldSelectModel was empty');

        if (!minimumConfidence.value) throw new Error('minimumConfidence was empty');
        if (!minimumSupport.value) throw new Error('minimumSupport was empty');
        if (!itemCount.value) throw new Error('itemCount was empty');

        fetchApriori({
          minimumConfidence: Number(minimumConfidence.value),
          minimumSupport: Number(minimumSupport.value),
          itemCount: Number(itemCount.value),
          databaseName: databaseSelectModel.value,
          collectionName: collectionSelectModel.value,
          columnName: fieldSelectModel.value[0],
        });
      },
      textAreaText,
      minimumSupport,
      minimumConfidence,
      itemCount,
      databaseSelectValues,
      databaseSelectModel,
      collectionSelectValues,
      collectionSelectModel,
      fieldSelectModel,
      fieldSelectValues,
      fieldValues,
    };
  },
});
</script>
