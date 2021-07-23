package com.wu.studyniuke.dao.elasticSearch;

import com.wu.studyniuke.entity.DiscussPost;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * @author me
 * @create 2021-07-23-21:10
 */
@Repository
public interface DiscussPostRepository extends ElasticsearchRepository<DiscussPost, Integer> {
}
