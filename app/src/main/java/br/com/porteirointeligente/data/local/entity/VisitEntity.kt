package br.com.porteirointeligente.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus

/**
 * Entidade de Room que persiste uma [Visit] no banco de dados local.
 */
@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = OwnerEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["ownerId"])]
)
data class VisitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val ownerId: Long? = null,
    val nome: String,
    val documento: String,
    val apartamento: String,
    val telefone: String,
    val motivo: String,
    val dataEntrada: Long,
    val dataSaida: Long? = null,
    val status: VisitStatus = VisitStatus.ENTRADA_REGISTRADA
) {
    /**
     * Converte a entidade em modelo de domínio.
     */
    fun toDomain(): Visit = Visit(
        id = id,
        ownerId = ownerId,
        nome = nome,
        documento = documento,
        apartamento = apartamento,
        telefone = telefone,
        motivo = motivo,
        dataEntrada = dataEntrada,
        dataSaida = dataSaida,
        status = status
    )

    companion object {
        /**
         * Converte um modelo de domínio em entidade.
         */
        fun fromDomain(visit: Visit): VisitEntity = VisitEntity(
            id = visit.id,
            ownerId = visit.ownerId,
            nome = visit.nome,
            documento = visit.documento,
            apartamento = visit.apartamento,
            telefone = visit.telefone,
            motivo = visit.motivo,
            dataEntrada = visit.dataEntrada,
            dataSaida = visit.dataSaida,
            status = visit.status
        )
    }
}
