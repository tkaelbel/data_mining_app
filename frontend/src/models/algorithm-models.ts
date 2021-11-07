export interface AlgorithmBaseProperties {
  databaseName: string;
  collectionName: string;
  columnName: string;
}

export interface AprioriProperties extends AlgorithmBaseProperties{
  minimumConfidence: number;
  minimumSupport: number;
  itemCount: number;
}

export enum Algorithm {
  APRIORI = 'APRIORI',
  KMEANS = 'KMEANS',
  KNEAREST = 'KNEAREST',
}
