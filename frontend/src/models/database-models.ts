export interface Database {
    databaseName: string;
    collections: Array<string>;
  }
  
  export interface CollectionData {
    databaseName: string;
    collectionName: string;
    pagination: Pagination;
    data: Array<Array<KeyValue>>;
  }

  export interface Pagination {
    page: number;
    totalPages: number;
    size: number;
    totalCount: number;
  }
  
  export interface Fields {
    fields: Array<string>;
  }
  
  export interface KeyValue { 
    key: string;
    value: string | number | Date | Record<string, unknown> | Array<Record<string, unknown>>;
    type: string;
  }