package br.com.porteirointeligente.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import br.com.porteirointeligente.R
import br.com.porteirointeligente.databinding.ItemVisitBinding
import br.com.porteirointeligente.domain.model.Visit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class VisitAdapter : ListAdapter<Visit, VisitAdapter.VisitViewHolder>(VisitDiffCallback()) {

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitViewHolder {
        val binding = ItemVisitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VisitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VisitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VisitViewHolder(private val binding: ItemVisitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(visit: Visit) {
            val context = binding.root.context
            binding.textVisitanteNome.text = visit.nome
            binding.textVisitDestino.text = context.getString(R.string.home_label_apartamento) + " " + visit.apartamento
            binding.textVisitTime.text = timeFormat.format(Date(visit.dataEntrada))
        }
    }

    class VisitDiffCallback : DiffUtil.ItemCallback<Visit>() {
        override fun areItemsTheSame(oldItem: Visit, newItem: Visit): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Visit, newItem: Visit): Boolean = oldItem == newItem
    }
}
