package temperance.ql;

public enum SetFunction {
    IN {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            return sw.caseIn();
        }
    },
    
    NOT {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            return sw.caseNot();
        }
    },
    ;
    public static interface Switch<RESULT> {
        RESULT caseIn();
        RESULT caseNot();
    }
    public abstract <RESULT> RESULT each(Switch<RESULT> sw);
}
