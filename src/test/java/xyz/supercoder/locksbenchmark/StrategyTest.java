package xyz.supercoder.locksbenchmark;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class StrategyTest {
    @Test
    public void testParseStrategy() {
        String[] args = {"-r", "5", "-w", "5", "-R", "20", "-t", "100000"};
        Optional<Strategy> optionalStrategy = Strategy.parseStrategy(args);

        Assert.assertTrue(optionalStrategy.isPresent());

        Strategy strategy = optionalStrategy.get();
        Assert.assertEquals(5, strategy.getReaderThreads());
        Assert.assertEquals(5, strategy.getWriterThreads());
        Assert.assertEquals(20, strategy.getRounds());
        Assert.assertEquals(100000, strategy.getTargetValue());
    }

    @Test
    public void testHelpStrategy() {
        String[] args = {"-h"};
        Optional<Strategy> optionalStrategy = Strategy.parseStrategy(args);

        Assert.assertFalse(optionalStrategy.isPresent());
    }

    @Test
    public void testDefaultStategy() {
        Optional<Strategy> optionalStrategy = Strategy.parseStrategy(null);

        Assert.assertTrue(optionalStrategy.isPresent());

        Strategy strategy = optionalStrategy.get();
        Assert.assertEquals(1, strategy.getReaderThreads());
        Assert.assertEquals(1, strategy.getWriterThreads());
        Assert.assertEquals(5, strategy.getRounds());
        Assert.assertEquals(1000000, strategy.getTargetValue());
    }

    @Test
    public void testInvalidArgs() {
        String[] args = {"-s"};
        Optional<Strategy> optionalStrategy = Strategy.parseStrategy(args);

        Assert.assertFalse(optionalStrategy.isPresent());
    }
}
