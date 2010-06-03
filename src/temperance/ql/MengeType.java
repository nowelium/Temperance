package temperance.ql;

public enum MengeType {
    // alias AND
    IN {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // AND
            return sw.caseAnd(param);
        }
    },
    
    AND {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // AND
            return sw.caseAnd(param);
        }
    },
    
    NOT {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // NOT
            return sw.caseNot(param);
        }
    },
    
    OR {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // OR
            return sw.caseOr(param);
        }
    },
    
    NAND {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // NAND
            throw new RuntimeException("not yet implemented");
        }
    },
    
    NOR {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // NOR
            throw new RuntimeException("not yet implemented");
        }
    },
    
    XOR {
        public <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param){
            // XOR
            throw new RuntimeException("not yet implemented");
        }
    }
    
    ;
    
    public abstract <RESULT, PARAMETER> RESULT each(Switch<RESULT, PARAMETER> sw, PARAMETER param);
    
    public static interface Switch<RESULT, PARAMETER> {
        RESULT caseAnd(PARAMETER param);
        RESULT caseNot(PARAMETER param);
        RESULT caseOr(PARAMETER param);
    }
}
