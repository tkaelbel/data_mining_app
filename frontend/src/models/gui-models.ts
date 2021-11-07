export interface TreeData {
  label: string;
  children: Array<TreeChildData>;
}

export interface TreeChildData {
  label: string;
  handler: (node: TreeChildData) => void;
}
