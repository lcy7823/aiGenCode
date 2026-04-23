package com.kaoyu.aicodebackend.facade.parser;

public interface CodeParser<T> {


    /**
     * 代码解析器
     *
     * @param codeContent
     * @return
     */
    T codeParse(String codeContent);

}
