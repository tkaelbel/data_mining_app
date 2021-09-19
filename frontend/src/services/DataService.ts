import { apiClient } from '../boot/axios';
import { CollectionData, Database } from 'src/components/models';
import { AxiosResponse } from 'axios';

class DataService {
  async getDatabases(): Promise<AxiosResponse<Array<Database>>> {
    return apiClient.get('/databases');
  }

  async getCollectionData(database: string, collection: string, page?: number): Promise<AxiosResponse<CollectionData>> {
    return apiClient.get('/collectionData', {
      params: { databaseName: database, collectionName: collection , page: page },
    });
  }
}

export default new DataService();
