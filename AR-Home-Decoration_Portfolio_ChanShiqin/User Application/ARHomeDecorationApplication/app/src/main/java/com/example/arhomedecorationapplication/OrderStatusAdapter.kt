import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.arhomedecorationapplication.OrderStatus
import com.example.arhomedecorationapplication.R

class OrderStatusAdapter(private val orderStatusList: List<OrderStatus>) :
    RecyclerView.Adapter<OrderStatusAdapter.OrderStatusViewHolder>() {

    class OrderStatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val statusText: TextView = view.findViewById(R.id.shippingStatusText)
        val dateTimeText: TextView = view.findViewById(R.id.shippingDateTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderStatusViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shipping_status, parent, false)
        return OrderStatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderStatusViewHolder, position: Int) {
        val currentItem = orderStatusList[position]
        holder.statusText.text = currentItem.updateStatus
        holder.dateTimeText.text = currentItem.updateTime
    }

    override fun getItemCount() = orderStatusList.size
}
