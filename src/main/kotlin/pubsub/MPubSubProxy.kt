//package pubsub
//
//import org.zeromq.ZMQ
//import zmq.ZMQ.term
//import zmq.ZMQ.socket
//
//
//
//fun configure_proxy2(context: ZMQ.Context) {
//    val xSub = context.socket(ZMQ.SUB)
//    xSub.bind("tcp://*:6000")
//    xSub.subscribe(ByteArray(0))
//    val xPub = context.socket(ZMQ.PUB)
//    xPub.bind("tcp://*:6001")
//    ZMQ.proxy(xSub, xPub, null)
//
//}
//
//fun other.main() {
//    val ctx = ZMQ.context(2)
//
//    configureProxy(ctx)
//}
//
//
//@JvmStatic
//fun other.main(args: Array<String>) {
//
//    val context = ZMQ.context(1)
//    val frontend = context.socket(ZMQ.SUB)
//    //		frontend.bind("tcp://*:9999");
//    frontend.bind("ipc://frontend.ipc")
//    frontend.subscribe("".toByteArray())
//
//    val backend = context.socket(ZMQ.PUB)
//    backend.bind("tcp://*:8888")
//
//    try {
//        println("Starting forwarder")
//        ZMQ.proxy(frontend, backend, null)
//    } catch (e: Exception) {
//        System.err.println(e.message)
//    } finally {
//        frontend.close()
//        backend.close()
//        context.term()
//    }
//
//}