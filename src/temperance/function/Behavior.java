package temperance.function;

public enum Behavior {
    
    Select {
        public <RESULT> RESULT each(Switch<RESULT> sw){
            return sw.caseSelect();
        }
    },
    Delete {
        public <RESULT> RESULT each(Switch<RESULT> sw) {
            return sw.caseDelete();
        }
    },
    ;
    
    public abstract <RESULT> RESULT each(Switch<RESULT> sw);
    
    public static interface Switch<RESULT> {
        public RESULT caseSelect();
        public RESULT caseDelete();
    }
}
