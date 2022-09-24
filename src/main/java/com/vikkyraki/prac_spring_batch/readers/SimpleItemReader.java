package com.vikkyraki.prac_spring_batch.readers;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimpleItemReader implements ItemReader<String> {

    private List<String> dataSet = new ArrayList<>();

    private Iterator<String> iterator;

    public  SimpleItemReader() {
        this.dataSet = Arrays.asList("1", "2", "3", "4", "5");
        this.iterator = dataSet.iterator();
    }

    // will be called, until whole data set is exhuasted
    @Override
    public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        System.out.println("Current item is " + (iterator.hasNext() ? iterator.next() : "empty"));

        return iterator.hasNext() ? iterator.next() : null;
    }
}
