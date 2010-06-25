package temperance.ql;

import temperance.function.InternalFunction;

public enum FunctionType {
    
    DATA {
        public InternalFunction create(Factory factory){
            return factory.createData();
        }
    },
    
    VALUE {
        public InternalFunction create(Factory factory){
            return factory.createValue();
        }
    },
    
    GEOPOINT {
        public InternalFunction create(Factory factory){
            return factory.createGeoPoint();
        }
    },
    
    MECAB {
        public InternalFunction create(Factory factory){
            return factory.createMecab();
        }
    },
    
    BIGRAM {
        public InternalFunction create(Factory factory){
            return factory.createBigram();
        }
    },
    
    GRAM {
        public InternalFunction create(Factory factory){
            return factory.createGram();
        }
    },
    
    PREFIX {
        public InternalFunction create(Factory factory){
            return factory.createPrefix();
        }
    },
    
    CSV {
        public InternalFunction create(Factory factory){
            return factory.createCSV();
        }
    },
    
    TSV {
        public InternalFunction create(Factory factory){
            return factory.createTSV();
        }
    },
    
    SSV {
        public InternalFunction create(Factory factory){
            return factory.createSSV();
        }
    },
    
    LevenshteinDistance {
        public InternalFunction create(Factory factory){
            return factory.createLevenshteinDistance();
        }
    }
    ;
    
    public abstract InternalFunction create(Factory factory);
    
    public interface Factory {
        public InternalFunction createData();
        public InternalFunction createValue();
        public InternalFunction createGeoPoint();
        public InternalFunction createMecab();
        public InternalFunction createBigram();
        public InternalFunction createGram();
        public InternalFunction createPrefix();
        public InternalFunction createCSV();
        public InternalFunction createTSV();
        public InternalFunction createSSV();
        public InternalFunction createLevenshteinDistance();
    }

}
