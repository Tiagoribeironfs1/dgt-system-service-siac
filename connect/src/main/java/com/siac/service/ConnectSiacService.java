package com.siac.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.siac.controller.QueryController;


@Service
public class ConnectSiacService {

    @Autowired
    public ConnectSiacService(QueryController myRepository) {
    }

    public void realizarOperacoesBancoDados() {
    }


}
