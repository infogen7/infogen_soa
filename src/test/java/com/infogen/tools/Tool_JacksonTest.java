package com.infogen.tools;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Clock;

import org.springframework.web.bind.annotation.RestController;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
public class Tool_JacksonTest {

  @Test(groups = { "tools","all" })
  public void toJson() {
    Pojo p = new Pojo();
    p.setCt(new Timestamp(Clock.system(Pojo.zoneid).millis()));
    p.setI(1);
    p.getList().add("list_num_1");
    p.getList().add("list_num_2");
    p.getMap().put("key1", "map_value_1");
    p.getMap().put("key2", "map_value_2");
    p.setName("JSON测试");
    p.setVar_boolean(false);
    p.setVar_byte((byte)1);
    p.setVar_char('a');
    p.setVar_double(0.5);
    p.setVar_float(0.5f);
    p.setVar_long(100L);
    p.setVer_short((short)1);
    String jsonString = Tool_Jackson.toJson(p);
    Assert.assertEquals(jsonString.startsWith("{"), true);
    Assert.assertEquals(jsonString.endsWith("}"), true);
    Assert.assertEquals(jsonString.contains("JSON测试"), true);
  }

  @Test(groups = { "tools","all" })
  public void toObjectStringClassT() throws JsonParseException, JsonMappingException, IOException {
    String jsonString = "{\"i\":1,\"ver_short\":1,\"var_char\":\"a\",\"var_byte\":1,\"var_boolean\":false,\"var_long\":100,\"var_float\":0.5,\"var_double\":0.5,\"Ct\":\"2015-04-21 09:55:23\",\"name\":\"JSON测试\",\"list\":[\"list_num_1\",\"list_num_2\"],\"map\":{\"key1\":\"map_value_1\",\"key2\":\"map_value_2\"},\"ct\":\"2015-04-21 09:55:23\"}";
    Pojo object = (Pojo)Tool_Jackson.toObject(jsonString, Pojo.class);
    Assert.assertEquals(object instanceof Pojo, true);
    
  }

  @Test(groups = { "tools","all" })
  public void toObjectStringTypeReferenceT() throws JsonParseException, JsonMappingException, IOException {
	String json = "{\"i\":1,\"ver_short\":1,\"var_char\":\"a\",\"var_byte\":1,\"var_boolean\":false,\"var_long\":100,\"var_float\":0.5,\"var_double\":0.5,\"Ct\":\"2015-04-21 09:55:23\",\"name\":\"JSON测试\",\"list\":[\"list_num_1\",\"list_num_2\"],\"map\":{\"key1\":\"map_value_1\",\"key2\":\"map_value_2\"},\"ct\":\"2015-04-21 09:55:23\",\"subname\":\"JSON-SUB\"}";
    Tool_Jackson.toObject(json, Pojo.class);
  }
}
