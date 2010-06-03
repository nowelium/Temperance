package temperance.ql;

public enum MengeType {
    IN {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // AND
            return sw.caseAnd();
        }
    },
    
    NOT {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // NOT
            return sw.caseNot();
        }
    },
    
    OR {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // OR
            throw new RuntimeException("not yet implemented");
        }
    },
    
    NAND {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // NAND
            throw new RuntimeException("not yet implemented");
        }
    },
    
    NOR {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // NOR
            throw new RuntimeException("not yet implemented");
        }
    },
    
    XOR {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            // XOR
            throw new RuntimeException("not yet implemented");
        }
    }
    
    ;
    public static interface Switch<RESULT> {
        RESULT caseAnd();
        RESULT caseNot();
    }
    public abstract <RESULT> RESULT each(Switch<RESULT> sw);
}
