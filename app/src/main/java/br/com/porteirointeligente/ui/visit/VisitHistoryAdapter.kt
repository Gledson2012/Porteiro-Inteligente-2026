package br.com.porteirointeligente.ui.visit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.porteirointeligente.databinding.ItemVisitHistoryBinding
import br.com.porteirointeligente.domain.model.Visit
import br.com.porteirointeligente.domain.model.VisitStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisitHistoryAdapter(
    private val onExitClick: (Visit) -> Unit
) : ListAdapter<Visit, VisitHistoryAdapter.ViewHolder>(VisitDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemVisitHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemVisitHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visit: Visit) {
            binding.textNome.text = visit.nome
            binding.textInfo.text = "Ap ${visit.apartamento} • Doc: ${visit.documento}"
            binding.textEntrada.text = "Entrada: ${timeFormat.format(Date(visit.dataEntrada))}"
            
            if (visit.status == VisitStatus.SAIDA_REGISTRADA && visit.dataSaida != null) {
                binding.textSaida.visibility = View.VISIBLE
                binding.textSaida.text = "Saída: ${timeFormat.format(Date(visit.dataSaida))}"
                binding.buttonRegistrarSaida.visibility = View.GONE
                binding.textStatus.text = "CONCLUÍDO"
                binding.textStatus.alpha = 0.5f
            } else {
                binding.textSaida.visibility = View.GONE
                binding.buttonRegistrarSaida.visibility = View.VISIBLE
                binding.textStatus.text = "NO PRÉDIO"
                binding.textStatus.alpha = 1.0f
            }

            binding.buttonRegistrarSaida.setOnClickListener { onExitClick(visit) }
        }
    }

    class VisitDiffCallback : DiffUtil.ItemCallback<Visit>() {
        override fun areItemsTheSame(oldItem: Visit, newItem: Visit): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Visit, newItem: Visit): Boolean = oldItem == newItem
    }
}
