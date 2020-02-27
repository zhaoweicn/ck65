package com.km.eda51_demo.data;

public class Part {
    private String partcode;
    private String partname;
    private String enginetype;

    // 零件编号
    public String getPartcode(){
        return partcode;
    }

    public void setPartcode(String partcode){
        this.partcode = partcode;
    }

    // 零件名称
    public String getPartname(){
        return partname;
    }

    public void setPartname(String partname){
        this.partname = partname;
    }

    // 发动机型号
    public String getEnginetype(){
        return enginetype;
    }

    public void setEnginetype(String enginetype){
        this.enginetype = enginetype;
    }
}
