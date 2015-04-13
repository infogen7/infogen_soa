namespace java com.infogen.thrift
namespace csharp com.infogen.thrift

struct Request{
	1:i32 crcCode=1,
	2:string sessionID,
	3:i64 sequence,
	4:string method,
	5:map<string,string> parameters	
}

struct Response{
	1:i32 crcCode=1,
	2:string sessionID,
	3:i64 sequence,
	4:string method,
	5:string data,
	6:bool success,
	7:string note,
	8:i32 code
}

service Message
{
        Response call(1:Request request);
}