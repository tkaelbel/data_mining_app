import { apiClient } from '../boot/axios';
import { CollectionData, Database, Fields } from 'src/components/models';
import { AxiosResponse } from 'axios';

class DataService {
  async getDatabases(): Promise<AxiosResponse<Array<Database>>> {
    return apiClient.get('/databases');
  }

  async getCollectionData(databaseName: string, collectionName: string, page?: number): Promise<AxiosResponse<CollectionData>> {
    return apiClient.get('/collectionData', {
      params: { databaseName, collectionName, page },
    });
  }

  async getFields(databaseName: string, collectionName: string): Promise<AxiosResponse<Fields>>{
    return apiClient.get('/fields', {
      params: { databaseName, collectionName }
    })
  }
}

export default new DataService();
