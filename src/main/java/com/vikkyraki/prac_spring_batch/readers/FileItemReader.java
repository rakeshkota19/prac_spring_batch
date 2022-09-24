package com.vikkyraki.prac_spring_batch.readers;

import com.vikkyraki.prac_spring_batch.repository.build.OrderFieldSetMapper;
import com.vikkyraki.prac_spring_batch.repository.model.Order;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class FileItemReader  {
    private static FlatFileItemReader<Order> itemReader;

    @Value("classpath:shipped_orders.csv")
    public  Resource shippedOrdersCsv;
    public String[] tokens = new String[] {"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};

    public FlatFileItemReader<Order> getFlatFileItemReader() {

        itemReader = new FlatFileItemReader<Order>();
        itemReader.setLinesToSkip(1);
        itemReader.setResource(shippedOrdersCsv);

        // how to parse the data from csv
        DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<>();
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(",");

        // takes the columns names from csv header
        tokenizer.setNames(tokens);

        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new OrderFieldSetMapper());

        itemReader.setLineMapper(lineMapper);
        return itemReader;

    }
}
