//
//
//
//import com.mongodb.client.MongoCollection;
//import com.plantronics.platform.common.bean.event.EventCategory;
//import com.plantronics.platform.core.persistence.model.document.event.CsCallEvent;
//import com.plantronics.platform.core.persistence.model.document.event.CsEvent;
//import com.plantronics.platform.core.repository.mongodb.events.EventRepository;
//import com.plantronics.platform.data.schema.*;
//import kafka.producer.ProducerConfig;
//import org.apache.commons.io.IOUtils;
//import org.json.JSONObject;
//import org.junit.Before;
//import javax.inject.Inject;
//import java.io.BufferedReader;
//import java.io.FileReader;
//import java.io.InputStream;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Properties;
//import java.util.regex.Matcher;
//import org.junit.Test;
//
//import org.apache.commons.io.IOUtils.*;
//import org.junit.runner.RunWith;
//
//import static org.mockito.Mockito.when;
//
//import org.mockito.Matchers;
//import org.mockito.Mock;
//import org.springframework.data.mongodb.core.mapping.Document;
//import org.springframework.data.mongodb.core.query.*;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
//import org.springframework.test.context.support.AnnotationConfigContextLoader;
//
///**
// * Test case for Storm com.plantronics.data.storm.bolts and com.plantronics.data.storm.spouts for call events over time.
// */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations={"/test-context-repository.xml"})
//public class CallEventsOverTimeTest{
//    @Inject
//    private EventRepository eventRepository;
//
//    @Before
//    public void initData(){
//        List<CsEvent> callEvents= new ArrayList<CsEvent>();
////        callEvents= getAllCallEvents();
////        try {
////            List<CsEvent> rawCallData = new ArrayList();
////            rawCallData = readMongoBson();
//////            when(eventRepository.findAll(Matchers.any(Query.class), Matchers.anyString())).thenReturn(readMongoBson());
////        } catch (Exception e) {
////
////        }
//
//
////        rawCallData.addAll((Collection<? extends CallEvent>) getAllCallEvents());
//
//        //initialize kafka test
//        Properties props = new Properties();
//        props.put("metadata.broker.list", "127.0.0.1:9092");
//        props.put("serializer.class", "kafka.serializer.DefaultEncoder");
//        ProducerConfig config = new ProducerConfig(props);
//    }
//
//    private List<CsEvent> readMongoBson() throws Exception{
//        String inputFilename; // initialize to the path of the file to read from
//        ArrayList<CsEvent> csEventList= new ArrayList<CsEvent>();
////        InputStream is =
////                CallEventsOverTimeTest.class.getResourceAsStream( "client38callevents.json");
////        String jsonTxt = IOUtils.toString( is );
////        JSONObject json = (JSONObject) JSONSerializer.toJSON( jsonTxt );
//        return csEventList;
//    }
//
//    @Test
//    public void testTopology() throws InterruptedException {
//             CallEventsOverTimeTest test= new CallEventsOverTimeTest();
//             test.initData();
//
////            //Topology definition
////            TopologyBuilder builder = new TopologyBuilder();
////            builder.setSpout("call-event-spout",new WordReader());
////            builder.setBolt("call-event-normalizer", new WordNormalizer())
////                    .shuffleGrouping("word-reader");
////            builder.setBolt("word-counter", new WordCounter(),1)
////                    .fieldsGrouping("word-normalizer", new Fields("word"));
////
////            //Configuration
////            Config conf = new Config();
////            conf.put("wordsFile", args[0]);
////            conf.setDebug(false);
////            //Topology run
////            conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
////            LocalCluster cluster = new LocalCluster();
////            cluster.submitTopology("Getting-Started-Toplogie", conf, builder.createTopology());
////            Thread.sleep(1000);
////            cluster.shutdown();
//        }
//
//    public List<CsEvent> getAllCallEvents() {
//        Query query =new Query();
//        query.addCriteria(Criteria.where("name").is("Eric"));
//        List<CsEvent> callEvents=eventRepository.findAll(query, EventCategory.CALL.getValue());
//        return new ArrayList<CsEvent>();
//    }
//}
