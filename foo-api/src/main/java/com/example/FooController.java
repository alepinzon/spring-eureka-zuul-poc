package com.example;

import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.document.UpdateItemOutcome;
import com.amazonaws.services.dynamodbv2.document.spec.UpdateItemSpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.dynamodbv2.model.ReturnValue;
import com.netflix.discovery.EurekaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;


@RestController
public class FooController {

    @Autowired
    @Lazy
    private EurekaClient eurekaClient;

    @Autowired
    private DynamoDB dynamoDB;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ErrorRepository errorRepository;

    @Value("${spring.application.name}")
    private String appName;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/greeting")
    public String greeting() {
        return String.format("Hello from '%s':'%s'!", eurekaClient.getApplication(appName).getName(), serverPort);
    }

    @GetMapping("/sequence")
    public String sequence() {

        final Table applications = dynamoDB.getTable("my-dynamodb-table");

        final UpdateItemSpec updateItemSpec = new UpdateItemSpec()
                .withPrimaryKey("id", "100")
                .withUpdateExpression("SET sequence_number = sequence_number + :incr")
                .withValueMap(new ValueMap().withNumber(":incr", 1))
                .withReturnValues(ReturnValue.UPDATED_NEW);

        System.out.println("Incrementing an atomic counter...");
        UpdateItemOutcome outcome = applications.updateItem(updateItemSpec);
        System.out.println("UpdateItem succeeded:\n" + outcome.getItem().toJSONPretty());

        final String instanceName = String.format("%s:%s", eurekaClient.getApplication(appName).getName(), serverPort);
        final long sequenceNumber = outcome.getItem().getLong("sequence_number");

        sleep();

        final Transaction transaction = fillTransaction(instanceName, sequenceNumber);
        try {
            transactionRepository.save(transaction);
            return String.format("Sequence generated by: %s - Sequence value: '%d'", instanceName, sequenceNumber);
        } catch (Exception exception) {
            errorRepository.save(fillError(instanceName, sequenceNumber));
            System.out.println(String.format("ERROR to persist the sequence generated by: %s - Sequence value: '%d'", instanceName, sequenceNumber));

            throw exception;
        }
    }

    private Transaction fillTransaction(final String instanceName, final Long sequenceNumber) {
        final Transaction transaction = new Transaction();
        transaction.setInstanceName(instanceName);
        transaction.setSequenceNumber(sequenceNumber);
        return transaction;
    }

    private Error fillError(final String instanceName, final Long sequenceNumber) {
        final Error error = new Error();
        error.setInstanceName(instanceName);
        error.setSequenceNumber(sequenceNumber);
        return error;
    }

    private void sleep() {
        long transactionTime = new Random().nextInt(6) * 1000;
        try {
            Thread.sleep(transactionTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
