export interface Todo {
  id: number;
  content: string;
}

export interface Meta {
  totalCount: number;
}

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

export interface TreeData {
  label: string;
  children: Array<TreeChildData>;
}

export interface TreeChildData {
  label: string;
  handler: (node: TreeChildData) => void;
}

export interface Pagination {
  page: number;
  totalPages: number;
  size: number;
  totalCount: number;
}

export interface KeyValue { 
  key: string;
  value: string | number | Date | Record<string, unknown> | Array<Record<string, unknown>>;
  type: string;
}