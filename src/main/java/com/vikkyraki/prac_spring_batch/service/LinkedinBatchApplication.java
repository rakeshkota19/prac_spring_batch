package com.vikkyraki.prac_spring_batch.service;

import com.vikkyraki.prac_spring_batch.readers.FileItemReader;
import com.vikkyraki.prac_spring_batch.repository.model.Order;
import com.vikkyraki.prac_spring_batch.repository.build.OrderRowMapper;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.List;

@Service
public class LinkedinBatchApplication {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

//    public FlatFileItemReader<Order> itemReader;

    public ItemReader<Order> itemReader;

    @Autowired
    public DataSource dataSource;

    public static String ORDER_SQL = "select order_id, first_name, last_name, "
            + "email, cost, item_id, item_name, ship_date "
            + "from SHIPPED_ORDER order by order_id";


    @Bean
    private PagingQueryProvider queryProvider() throws Exception {
        SqlPagingQueryProviderFactoryBean factoryBean = new SqlPagingQueryProviderFactoryBean();

        factoryBean.setSelectClause("select order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
        factoryBean.setFromClause("from SHIPPED_ORDER");
        factoryBean.setSortKey("order_id");
        factoryBean.setDataSource(dataSource);

        return factoryBean.getObject();
    }

    @PostConstruct
    private void __init() throws Exception {
//        itemReader = new FileItemReader().getFlatFileItemReader();
//
//        itemReader = new JdbcCursorItemReaderBuilder<Order>()
//                            .dataSource(dataSource)
//                            .name("jdbcCursorItemReader")
//                            .sql(ORDER_SQL)
//                            .rowMapper(new OrderRowMapper()).build();

        itemReader = new JdbcPagingItemReaderBuilder<Order>().dataSource(dataSource).name("jdbcPagingItemReader").
                            queryProvider(queryProvider()).rowMapper(new OrderRowMapper()).pageSize(10).build();
    }

    @Bean
    public Step chunkBasedStep() {
        return this.stepBuilderFactory.get("chunkBasedStep").
                                        <Order, Order>chunk(3).
                                        reader(itemReader).
                                         writer(new ItemWriter<Order>() {
                                             @Override
                                            public void write(List<? extends Order> list) throws Exception {
                                                System.out.println(String.format("Received list size" + list.size()));
                                                list.forEach(System.out::println);
                                            }
                                        }).build();
    }

    @Bean
    public Job job() throws Exception {
        return this.jobBuilderFactory.get("job")
                .start(chunkBasedStep())
                .build();
    }

    public static void main(String[] args) {
        SpringApplication.run(LinkedinBatchApplication.class, args);
    }

}