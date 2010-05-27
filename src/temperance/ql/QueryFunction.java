package temperance.ql;

public enum QueryFunction {
    
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
            return factory.createGeoPoint();
        }
    },
    ;
    
    public interface Factory {
        public InternalFunction createData();
        public InternalFunction createValue();
        public InternalFunction createGeoPoint();
        public InternalFunction createMecab();
        public InternalFunction createBigram();
        public InternalFunction createGram();
    }
    public abstract InternalFunction create(Factory factory);

}
