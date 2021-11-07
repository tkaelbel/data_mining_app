import { baseURL } from '../boot/axios';
import { Algorithm, AprioriProperties } from 'src/models/algorithm-models';

const prefix = 'algorithm';
const uriApriori = '/apriori';

class AlgorithmService {
  apriori(aprioriProperties: AprioriProperties): EventSource {
    const baseUri = `${baseURL}${prefix}${uriApriori}?`;
    const paraName = `name=${Algorithm.APRIORI}`;
    const paraMinConf = `&minimumConfidence=${aprioriProperties.minimumConfidence}`;
    const paraMinSup = `&minimumSupport=${aprioriProperties.minimumSupport}`;
    const paraItemCount = `&itemCount=${aprioriProperties.itemCount}`;
    const paraDbName = `&databaseName=${aprioriProperties.databaseName}`;
    const paraCollectionName = `&collectionName=${aprioriProperties.collectionName}`;
    const paraColumnName = `&columnName=${aprioriProperties.columnName}`;

    return new EventSource(
      `${baseUri}${paraName}${paraMinConf}${paraMinSup}${paraItemCount}${paraDbName}${paraCollectionName}${paraColumnName}`
    );
  }
}

export default new AlgorithmService();
