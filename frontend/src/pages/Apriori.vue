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
            (val) => (val && val.length > 0) || 'One number at least',
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
            (val) => val >= 0 || 'Positive number',
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
import { Algorithm, AprioriProperties, Database } from 'src/components/models';
import DataService from 'src/services/DataService';
import AlgorithmService from 'src/services/AlgorithmService';

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

    let interval: any;

    //TODO: array for fields
    const fetchApriori = async (
      minimumConfidence: number,
      minimumSupport: number,
      itemCount: number,
      database: string,
      collection: string,
      field: string
    ) => {
      try {
        const apriorData = await AlgorithmService.apriori(
          minimumConfidence,
          minimumSupport,
          itemCount,
          database,
          collection,
          field
        );

        textAreaText.value = apriorData.data;
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
      async onSubmit() {
        if (!databaseSelectModel.value) throw new Error('');
        if (!collectionSelectModel.value) throw new Error('');
        if (!fieldSelectModel.value) throw new Error('');

        if (!minimumConfidence.value) throw new Error('');
        if (!minimumSupport.value) throw new Error('');
        if (!itemCount.value) throw new Error('');

        const tempMinimumConfidence = Number(minimumConfidence.value);
        const tempMinimumSupport = Number(minimumSupport.value);
        const tempItemCount = Number(itemCount.value);

        const tempDatabaseName = databaseSelectModel.value;
        const tempCollectionName = collectionSelectModel.value;
        const tempFieldName = fieldSelectModel.value[0];

        await fetchApriori(
          tempMinimumConfidence,
          tempMinimumSupport,
          tempItemCount,
          tempDatabaseName,
          tempCollectionName,
          tempFieldName
        );

        // interval = setInterval(() => fetchApriori(
        //   {
        //     minimumConfidence: tempMinimumConfidence,
        //     minimumSupport: tempMinimumSupport,
        //     itemCount: tempItemCount,
        //   },
        //   tempDatabaseName,
        //   tempCollectionName,
        //   tempFieldName,
        // ), 1000);
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
