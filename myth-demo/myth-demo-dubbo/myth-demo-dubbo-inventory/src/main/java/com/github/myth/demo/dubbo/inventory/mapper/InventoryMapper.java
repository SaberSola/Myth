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

package com.github.myth.demo.dubbo.inventory.mapper;

import com.github.myth.demo.dubbo.inventory.api.entity.Inventory;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author xiaoyu
 */
public interface InventoryMapper {



    /**
     * 库存扣减
     *
     * @param inventory 实体对象
     * @return rows
     */
    @Update("update inventory set total_inventory =#{totalInventory}" +
            " where product_id =#{productId}  and  total_inventory >0  ")
    int decrease(Inventory inventory);


    /**
     * 根据商品id找到库存信息
     *
     * @param productId 商品id
     * @return Inventory
     */
    @Select("select * from inventory where product_id =#{productId}")
    Inventory findByProductId(String productId);
}
