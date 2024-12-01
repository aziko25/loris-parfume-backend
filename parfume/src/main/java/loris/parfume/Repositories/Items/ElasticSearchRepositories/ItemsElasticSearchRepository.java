package loris.parfume.Repositories.Items.ElasticSearchRepositories;

import loris.parfume.Models.Items.Items_ElasticSearch;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemsElasticSearchRepository extends ElasticsearchRepository<Items_ElasticSearch, Long> {

    @Query("{\"bool\": {\"should\": [" +
            "{\"match\": {\"nameUz\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}, " +
            "{\"match\": {\"nameRu\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}, " +
            "{\"match\": {\"descriptionUz\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}, " +
            "{\"match\": {\"descriptionRu\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}}, " +
            "{\"match\": {\"barcode\": {\"query\": \"?0\", \"fuzziness\": \"AUTO\"}}} " +
            "]}}, \"size\": ?1}")
    List<Items_ElasticSearch> findAllByNameUz(String searchUz, int size);

    @Query("""
    {
      "bool": {
        "must": [
          { "term": { "isActive": true }},
          {
            "bool": {
              "should": [
                { "match": { "nameUz": { "query": "?0", "fuzziness": "AUTO" }}},
                { "match": { "nameRu": { "query": "?0", "fuzziness": "AUTO" }}},
                { "match": { "descriptionUz": { "query": "?0", "fuzziness": "AUTO" }}},
                { "match": { "descriptionRu": { "query": "?0", "fuzziness": "AUTO" }}},
                { "match": { "barcode": { "query": "?0", "fuzziness": "AUTO" }}}
              ]
            }
          }
        ]
      }},
      "size": "?1"
    }
    """)
    List<Items_ElasticSearch> findByMultiMatchAndIsActive(String search, int size);

    Optional<Items_ElasticSearch> findBySlug(String slug);
}