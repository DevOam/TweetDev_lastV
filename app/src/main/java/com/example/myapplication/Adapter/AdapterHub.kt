import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.myapplication.Hub_detail_screen
import com.example.myapplication.databinding.ItemHubBinding
import com.example.myapplication.model.Hub

class AdapterHubs(private val context: Context, private val hubs: List<Hub>, private val userId: String?) : RecyclerView.Adapter<AdapterHubs.HubViewHolder>() {

    class HubViewHolder(private val binding: ItemHubBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hub: Hub, userId: String?) {
            binding.nameUser.text = hub.name
            binding.nbrHubs.text = hub.users.size.toString()
            Glide.with(binding.root.context)
                .load(hub.profileImageUrl)
                .into(binding.imgProfile)
            Glide.with(binding.root.context)
                .load(hub.coverImageUrl)
                .into(binding.imageCover)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, Hub_detail_screen::class.java).apply {
                    putExtra("HUB_ID", hub._id)
                    putExtra("HUB_NAME", hub.name)
                    putExtra("HUB_DESCRIPTION", hub.description)
                    putExtra("HUB_PROFILE_IMAGE_URL", hub.profileImageUrl)
                    putExtra("HUB_COVER_IMAGE_URL", hub.coverImageUrl)
                    putExtra("HUB_CREATION_DATE", hub.creationDate)
                    putExtra("USER_ID", userId)  // Pass userId here
                }
                binding.root.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HubViewHolder {
        val binding = ItemHubBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HubViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HubViewHolder, position: Int) {
        holder.bind(hubs[position], userId)  // Pass userId here
    }

    override fun getItemCount(): Int = hubs.size
}
