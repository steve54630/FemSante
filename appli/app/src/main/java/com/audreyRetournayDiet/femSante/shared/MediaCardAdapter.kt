package com.audreyRetournayDiet.femSante.shared

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.audreyRetournayDiet.femSante.R
import com.audreyRetournayDiet.femSante.data.media.MediaItem
import com.audreyRetournayDiet.femSante.data.media.MediaType

/**
 * Grille de cartes de contenus bien-être (vidéo ou audio). Icône selon le type, libellé de
 * thème, et **cadenas si le contenu est premium ET que l'utilisatrice n'a pas l'accès**
 * ([userHasAccess]). Réutilisable par « Bien dans ta tête » et « Bien dans ton corps ».
 */
class MediaCardAdapter(
    private val userHasAccess: Boolean,
    private val onClick: (MediaItem) -> Unit
) : ListAdapter<MediaItem, MediaCardAdapter.MediaViewHolder>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_media_card, parent, false)
        return MediaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) = holder.bind(getItem(position))

    inner class MediaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.imageIcon)
        private val lock: ImageView = itemView.findViewById(R.id.imageLock)
        private val title: TextView = itemView.findViewById(R.id.textTitle)
        private val theme: TextView = itemView.findViewById(R.id.textTheme)

        fun bind(item: MediaItem) {
            title.text = item.title
            theme.text = item.category.label
            icon.setImageResource(
                if (item.type == MediaType.VIDEO) R.drawable.ic_play else R.drawable.ic_headphones
            )
            lock.visibility = if (item.premium && !userHasAccess) View.VISIBLE else View.GONE
            itemView.setOnClickListener { onClick(item) }
        }
    }

    private companion object {
        val DIFF = object : DiffUtil.ItemCallback<MediaItem>() {
            override fun areItemsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem.title == newItem.title
            override fun areContentsTheSame(oldItem: MediaItem, newItem: MediaItem) = oldItem == newItem
        }
    }
}
