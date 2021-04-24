package com.difx.srping.enums;

public enum ScopeEnum {
    PROTOTYPE(1, "prototype"),
    SINGLETON(2, "singleton");

    private final int id;
    private final String name;

    ScopeEnum(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public static ScopeEnum getByName(String name){
        for (ScopeEnum sp: values()){
            if (sp.name.equals(name)){
                return sp;
            }
        }
        return null;
    }
}
