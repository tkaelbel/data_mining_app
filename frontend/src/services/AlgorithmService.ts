import { apiClient } from '../boot/axios';
import {
  Algorithm,
  AprioriProperties,
  CollectionData,
  Database,
  Fields,
} from 'src/components/models';
import { AxiosResponse } from 'axios';

const prefix = 'algorithm';

class AlgorithmService {
  async apriori(
    minimumConfidence: number,
    minimumSupport: number,
    itemCount: number,
    databaseName: string,
    collectionName: string,
    columnName: string
  ): Promise<AxiosResponse<any>> {
    return apiClient.get(`${prefix}/apriori`, {
      params: {
        name: Algorithm.APRIORI,
        minimumConfidence,
        minimumSupport,
        itemCount,
        databaseName,
        collectionName,
        columnName,
      },
    });
  }
}

export default new AlgorithmService();
