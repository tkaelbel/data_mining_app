<template>
  <q-page>
    <q-splitter
      v-model="splitterModel"
      :limits="[150, 300]"
      unit="px"
      style="min-height: inherit"
    >
      <template v-slot:before>
        <div class="q-pa-md" v-if="isDatabaseDataFetched">
          <div style="display: flex; border-bottom: 1px solid black">
            <q-icon name="storage" size="25px" />
            <q-item-label class="q-pl-xs text-weight-bold text-h6"
              >Databases</q-item-label
            >
          </div>
          <q-tree
            no-connectors
            :nodes="treeData"
            node-key="label"
            selected-color="primary"
            style="min-height: inherit"
            class="space-xs"
            accordion="true"
            v-model:selected="handleTreeClick"
          />
        </div>
      </template>

      <template v-slot:after>
        <div v-if="isCollectionDataFetched">
          <q-tab-panels style="min-height: inherit" v-model="handleTreeClick">
            <q-tab-panel v-for="name in collectionNames" :name="name"  v-bind:key="name">
              <div class="text-h4 q-mb-md">{{ selectedCollectionName }}</div>
              <div class="text-h5 q-mb-md">
                Query Results 1-{{ collectionPaginationData?.size }} of
                {{ collectionPaginationData?.totalCount }}
              </div>

              <div v-if="collectionPaginationData">
                <div class="q-pa-lg flex flex-center">
                  <q-pagination
                    v-model="collectionPaginationData.page"
                    :max="collectionPaginationData.totalPages"
                    :max-pages="6"
                    @click="paginationClickHandler"
                  />
                </div>
              </div>

              <div
                v-for="documents in collectionData"
                v-bind:key="documents"
                style="border: 1px solid #1976d2"
                class="q-mb-md"
              >
                <div
                  v-for="document in documents"
                  v-bind:key="document"
                  style="
                    display: flex;
                    flex-direction: row;
                    flex-wrap: nowrap;
                    align-items: center;
                    font-family: monospace;
                    font-size: 11px;
                    text-overflow: ellipsis;
                  "
                  class="q-ml-md"
                >
                  <div class="text-weight-bold">{{ document.key }}</div>
                  <span class="text-weight-bold">:</span>

                  <div v-if="typeof document.value === 'object'">
                    <div>Object</div>
                  </div>

                  <div v-if="document.type === 'Date'">
                    <div class="document-field-date">{{ document.value }}</div>
                  </div>

                  <div
                    v-if="
                      document.type !== 'Date' &&
                      typeof document.value === 'string'
                    "
                  >
                    <div class="document-field-string">
                      "{{ document.value }}"
                    </div>
                  </div>

                  <div v-if="typeof document.value === 'number'">
                    <div class="document-field-number">
                      {{ document.value }}
                    </div>
                  </div>
                </div>
              </div>
            </q-tab-panel>
          </q-tab-panels>
        </div>
      </template>
    </q-splitter>
  </q-page>
</template>

<script lang="ts">
import {
  Database,
  KeyValue,
  Pagination,
  TreeChildData,
  TreeData,
} from 'src/components/models';
import { defineComponent, ref, onMounted} from 'vue';
import DataService from '../services/DataService';

export default defineComponent({
  name: 'Databases',
  setup() {
    const databases = ref();
    const treeData = ref<Array<TreeData>>();
    const collectionData = ref<Array<Array<KeyValue>>>([]);
    const collectionPaginationData = ref<Pagination>();
    const collectionNames = ref<Set<string>>(new Set());
    const selectedCollectionName = ref<string>();
    const selectedDatabaseName = ref<string>();
    const isDatabaseDataFetched = ref(false);
    const isCollectionDataFetched = ref(false);

    console.log(databases);

    onMounted(async () => {
      try {
        const data = await DataService.getDatabases();
        databases.value = data.data;

        treeData.value = buildTreeData(data.data);

        data.data.forEach((database: Database) => {
          database.collections.forEach((collection: string) =>
            collectionNames.value.add(collection)
          );
        });

        isDatabaseDataFetched.value = true;
      } catch (error) {
        console.error(error);
      }
    });

    const buildTreeData = (data: Database[]): Array<TreeData> => {
      const model: Array<TreeData> = [];
      data.forEach((database: Database) => {
        const children: Array<TreeChildData> = [];
        database.collections?.forEach((collection: string) => {
          children.push({ label: collection, handler: handleTreeClick });
        });

        model.push({ label: database.databaseName, children });
      });

      return model;
    };

    const handleTreeClick = async (node: TreeChildData) => {
      const realTreeData = treeData.value;
      const database = realTreeData?.find((data) =>
        data.children.some((item) => item.label === node.label)
      );

      if (!database)
        throw new Error(
          `Could not determine database for collection ${node.label}`
        );

      try {
        const { data } = await DataService.getCollectionData(
          database.label,
          node.label
        );

        isCollectionDataFetched.value = true;

        collectionPaginationData.value = data.pagination;
        selectedDatabaseName.value = database.label;

        collectionData.value = [];
        collectionData.value.push(...data.data);

        selectedCollectionName.value = node.label;

        console.log(collectionData);
      } catch (error) {
        console.error(error);
      }

      return node.label;
    };

    const paginationClickHandler = async (event: any) => {
      //Hack to get the number of the clicked page
      //TODO: maybe find an easier way..
      const page = event.target.innerText;

      if(page === "…") return;

      if(!selectedDatabaseName.value || !selectedCollectionName.value) throw new Error("Not possible that data")

      const { data } = await DataService.getCollectionData(
          selectedDatabaseName.value,
          selectedCollectionName.value,
          page,
        );

        collectionData.value = [];
        collectionData.value.push(...data.data);

        collectionPaginationData.value = data.pagination;
    }

    return {
      splitterModel: ref(200),
      databases,
      treeData,
      isDatabaseDataFetched,
      isCollectionDataFetched,
      collectionPaginationData,
      collectionData,
      collectionNames,
      selectedCollectionName,
      handleTreeClick,
      paginationClickHandler,
    };
  },
});
</script>
<style lang="scss">
.document-field-string {
  color: #4682b4;
  text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
    max-width: 1000px;
    display: block;
}

.document-field-date {
  color: #b22222;
}

.document-field-number {
  color: #229954;
}
</style>
