<template>
  <q-layout view="lHh Lpr lFf">
    <q-header elevated>
      <q-toolbar>
        <div class="lt-md">
          <q-btn
            flat
            dense
            round
            icon="menu"
            aria-label="Menu"
            @click="toggleLeftDrawer"
          />
        </div>

        <div>
          <q-img
            class="q-mr-lg"
            src="../statics/data-mining-app.png"
            width="250px"
            height="42px"
          />
        </div>

        <div class="gt-sm">
          <q-btn flat icon="storage" label="Databases" />
          <q-btn flat icon="functions" label="Algorithms" />
          <q-btn flat icon="import_export" label="Import" />
        </div>
      </q-toolbar>
    </q-header>

    <q-drawer v-model="leftDrawerOpen" bordered :width="200">
      <q-list padding>
        <q-item clickable>
          <q-item-section avatar>
            <q-icon name="storage" size="25px" />
          </q-item-section>
          <q-item-section>DATABASES</q-item-section>
        </q-item>

        <q-item clickable>
          <q-item-section avatar>
            <q-icon name="functions" size="25px" />
          </q-item-section>
          <q-item-section>ALGORITHMS</q-item-section>
        </q-item>

        <q-item clickable>
          <q-item-section avatar>
            <q-icon name="import_export" size="25px" />
          </q-item-section>
          <q-item-section>Import</q-item-section>
        </q-item>
      </q-list>
    </q-drawer>

    <q-page-container>
      <div>
        <router-view />
      </div>
    </q-page-container>
  </q-layout>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';

export default defineComponent({
  name: 'MainLayout',

  setup() {
    const leftDrawerOpen = ref(false);

    return {
      leftDrawerOpen,
      toggleLeftDrawer() {
        leftDrawerOpen.value = !leftDrawerOpen.value;
      },
    };
  },
});
</script>
