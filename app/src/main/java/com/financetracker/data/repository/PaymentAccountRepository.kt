package com.financetracker.data.repository

import com.financetracker.data.db.dao.PaymentAccountDao
import com.financetracker.data.db.entity.PaymentAccountEntity
import com.financetracker.domain.model.PaymentAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PaymentAccountRepository(private val dao: PaymentAccountDao) {

    fun getAll(): Flow<List<PaymentAccount>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    fun getEnabled(): Flow<List<PaymentAccount>> =
        dao.getEnabled().map { list -> list.map { it.toDomain() } }

    suspend fun getById(id: Long): PaymentAccount? =
        dao.getById(id)?.toDomain()

    suspend fun getByType(type: String): PaymentAccount? =
        dao.getByType(type)?.toDomain()

    suspend fun count(): Int = dao.count()

    suspend fun updateBalance(id: Long, balance: Double) = dao.updateBalance(id, balance)

    suspend fun seedIfEmpty() {
        if (dao.count() == 0) {
            listOf(
                PaymentAccountEntity(1, "微信支付", "wechat", true, "#07C160"),
                PaymentAccountEntity(2, "支付宝", "alipay", true, "#1677FF"),
                PaymentAccountEntity(3, "京东白条", "jd", true, "#E3312C"),
                PaymentAccountEntity(4, "银行卡", "bank", true, "#F5A623"),
            ).forEach { dao.insert(it) }
        }
    }
}

private fun PaymentAccountEntity.toDomain() = PaymentAccount(id, name, type, isEnabled, color, balance)
