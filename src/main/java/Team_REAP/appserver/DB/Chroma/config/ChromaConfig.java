//package Team_REAP.appserver.RAG.RAG.config;
//
//import org.springframework.ai.chroma.ChromaApi;
//import org.springframework.ai.embedding.EmbeddingModel;
//import org.springframework.ai.vectorstore.ChromaVectorStore;
//import org.springframework.ai.vectorstore.VectorStore;
//import org.springframework.context.annotation.Bean;
//
//public class ChromaConfig {
//
//    @Bean
//    public VectorStore chromaVectorStore(EmbeddingModel embeddingModel, ChromaApi chromaApi) {
//        return new ChromaVectorStore(embeddingModel, chromaApi, "TestCollection", false);
//    }
//}
