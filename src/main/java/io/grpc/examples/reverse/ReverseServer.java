package io.grpc.examples.reverse;

import io.grpc.Grpc;
import io.grpc.InsecureServerCredentials;
import io.grpc.Server;
import io.grpc.examples.helloworld.HelloWorldServer;
import io.grpc.stub.StreamObserver;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ReverseServer {

    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;

    private void start() throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        server = Grpc.newServerBuilderForPort(port, InsecureServerCredentials.create())
                .addService(new ReverseServer.ReverseImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            try {
                ReverseServer.this.stop();
            } catch (InterruptedException e) {
                e.printStackTrace(System.err);
            }
            System.err.println("*** server shut down");
        }));
    }

    private void stop() throws InterruptedException {

        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     * Daemon thread in Java is a low-priority thread that performs background operations such as garbage collection, finalizer, Action Listeners, Signal dispatches, etc
     */
    private void blockUntilShutdown() throws InterruptedException {

        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ReverseServer server = new ReverseServer();
        server.start();
        server.blockUntilShutdown();
    }

    static class ReverseImpl extends ReverseGrpc.ReverseImplBase {

        @Override
        public void reverseString(ReverseRequest req, StreamObserver<ReverseReply> responseObserver) {

            String str = req.getMessage();
            StringBuilder sb = new StringBuilder(str);
            sb.reverse();
            String reversedStr = sb.toString();

            ReverseReply reply = ReverseReply.newBuilder().setMessage("Hello " + reversedStr).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }


    }
              }
