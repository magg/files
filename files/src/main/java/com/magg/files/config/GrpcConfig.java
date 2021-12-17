package com.magg.files.config;

import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import java.util.concurrent.TimeUnit;
import net.devh.boot.grpc.server.serverfactory.GrpcServerConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class GrpcConfig {
    @Bean
    public GrpcServerConfigurer keepAliveServerConfigurer() {
        return serverBuilder -> {
            if (serverBuilder instanceof NettyServerBuilder) {
                ((NettyServerBuilder) serverBuilder)
                    .permitKeepAliveTime(150, TimeUnit.SECONDS)
                    .keepAliveTime(150, TimeUnit.SECONDS)
                    .keepAliveTimeout(150, TimeUnit.SECONDS)
                    .permitKeepAliveWithoutCalls(true);
            }
        };
    }
}
