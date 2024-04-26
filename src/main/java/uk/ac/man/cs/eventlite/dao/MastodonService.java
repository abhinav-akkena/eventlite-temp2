package uk.ac.man.cs.eventlite.dao;

import java.util.List;

import uk.ac.man.cs.eventlite.entities.MastodonPost;

public interface MastodonService {
    List<MastodonPost> fetchLastThreePosts();
    
    void shareStatus(String status) throws Exception;
}