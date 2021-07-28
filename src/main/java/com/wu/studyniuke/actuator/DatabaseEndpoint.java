package com.wu.studyniuke.actuator;

import com.wu.studyniuke.util.CommunityUtil;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


/**
 * @author me
 * @create 2021-07-28-11:45
 */
@Component
@Endpoint(id = "database")
public class DatabaseEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseEndpoint.class);

    @Qualifier("dataSource")
    @Autowired
    private DataSource dataSource;

    @ReadOperation
    public String checkConnection() {
        try (
                Connection connection = dataSource.getConnection();
        ) {
            return CommunityUtil.getJSONString(0, "连接成功");
        } catch (SQLException e) {
            LOGGER.error("连接失败");
            return CommunityUtil.getJSONString(1, "disconnect");
        }
    }
}
