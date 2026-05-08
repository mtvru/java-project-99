package hexlet.code.controller;

import hexlet.code.TestConfig;
import hexlet.code.TestModelFactory;
import hexlet.code.TestPersistenceManager;
import org.springframework.context.annotation.Import;

@Import({TestConfig.class, TestPersistenceManager.class, TestModelFactory.class})
public abstract class AbstractControllerTest {

}
