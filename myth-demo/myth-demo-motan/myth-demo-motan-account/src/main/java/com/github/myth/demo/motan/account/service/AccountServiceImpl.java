/*
 *
 * Copyright 2017-2018 549477611@qq.com(xiaoyu)
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, see <http://www.gnu.org/licenses/>.
 *
 */

package com.github.myth.demo.motan.account.service;

import com.github.myth.annotation.Myth;
import com.github.myth.demo.motan.account.api.dto.AccountDTO;
import com.github.myth.demo.motan.account.api.entity.AccountDO;
import com.github.myth.demo.motan.account.api.service.AccountService;
import com.github.myth.demo.motan.account.mapper.AccountMapper;
import com.weibo.api.motan.config.springsupport.annotation.MotanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author xiaoyu
 */
@MotanService
public class AccountServiceImpl implements AccountService {

    /**
     * logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);


    private final AccountMapper accountMapper;

    @Autowired(required = false)
    public AccountServiceImpl(AccountMapper accountMapper) {
        this.accountMapper = accountMapper;
    }

    /**
     * 扣款支付 真实的场景请保证幕等性。
     *
     * @param accountDTO 参数dto
     * @return true
     */
    @Override
    @Myth(destination = "account")
    public boolean payment(AccountDTO accountDTO) {
        LOGGER.debug("========================motan 框架 开始执行扣款服务===");
        final AccountDO accountDO = accountMapper.findByUserId(accountDTO.getUserId());
        if (accountDO.getBalance().compareTo(BigDecimal.valueOf(accountDTO.getAmount())) <= 0) {
            throw new RuntimeException("资金不足！");
        }
        accountDO.setBalance(accountDO.getBalance().subtract(
                BigDecimal.valueOf(accountDTO.getAmount())));
        accountDO.setUpdateTime(new Date());
        final int update = accountMapper.update(accountDO);
        if (update != 1) {
            throw new RuntimeException("资金不足！");
        }
        return Boolean.TRUE;
    }

    /**
     * 获取用户资金信息
     *
     * @param userId 用户id
     * @return AccountDO
     */
    @Override
    public AccountDO findByUserId(String userId) {
        return  accountMapper.findByUserId(userId);
    }

}
