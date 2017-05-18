package org.workspace7.moviestore;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.workspace7.moviestore.config.MovieStoreProps;
import org.workspace7.moviestore.utils.MovieDBHelper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author kameshs
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MovieDBHelperTest {

    @Autowired
    MovieStoreProps movieStoreProps;

    @Autowired
    MovieDBHelper movieDBHelper;

    @Autowired
    CacheManager cacheManager;

    @Test
    public void testMovieStoreProps() {
        assertThat(movieStoreProps).isNotNull();
        assertThat(movieStoreProps.getApiEndpointUrl()).isNotNull();
        assertThat(movieStoreProps.getApiEndpointUrl()).isEqualTo("https://api.themoviedb.org/3");
        assertThat(movieStoreProps.getApiKey()).isNotNull();
    }

//    @Test
//    public void testQueryAndCache() {
//        assertThat(movieDBHelper).isNotNull();
//        int statusCode = movieDBHelper.queryAndCache();
//        assertThat(statusCode).isEqualTo(200);
//        Cache<Long, Movie> moviesCache = (Cache<Long, Movie>) cacheManager
//            .getCache(MovieDBHelper.POPULAR_MOVIES_CACHE).getNativeCache();
//        assertThat(moviesCache.entrySet()).isNotEmpty();
//    }
}
