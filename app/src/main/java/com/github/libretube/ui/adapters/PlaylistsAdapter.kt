package com.github.libretube.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.libretube.R
import com.github.libretube.api.obj.Playlists
import com.github.libretube.databinding.PlaylistsRowBinding
import com.github.libretube.ui.base.BaseActivity
import com.github.libretube.ui.dialogs.DeletePlaylistDialog
import com.github.libretube.ui.sheets.PlaylistOptionsBottomSheet
import com.github.libretube.ui.viewholders.PlaylistsViewHolder
import com.github.libretube.util.ImageHelper
import com.github.libretube.util.NavigationHelper

class PlaylistsAdapter(
    private val playlists: MutableList<Playlists>
) : RecyclerView.Adapter<PlaylistsViewHolder>() {

    override fun getItemCount(): Int {
        return playlists.size
    }

    fun updateItems(newItems: List<Playlists>) {
        val oldSize = playlists.size
        playlists.addAll(newItems)
        notifyItemRangeInserted(oldSize, playlists.size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = PlaylistsRowBinding.inflate(layoutInflater, parent, false)
        return PlaylistsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistsViewHolder, position: Int) {
        val playlist = playlists[position]
        holder.binding.apply {
            // set imageview drawable as empty playlist if imageview empty
            if (playlist.thumbnail!!.split("/").size <= 4) {
                playlistThumbnail.setImageResource(R.drawable.ic_empty_playlist)
                playlistThumbnail.setBackgroundColor(R.attr.colorSurface)
            } else {
                ImageHelper.loadImage(playlist.thumbnail, playlistThumbnail)
            }
            playlistTitle.text = playlist.name

            videoCount.text = playlist.videos.toString()

            deletePlaylist.setOnClickListener {
                DeletePlaylistDialog(playlist.id!!) {
                    playlists.removeAt(position)
                    (root.context as BaseActivity).runOnUiThread {
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, itemCount)
                    }
                }.show(
                    (root.context as BaseActivity).supportFragmentManager,
                    null
                )
            }
            root.setOnClickListener {
                NavigationHelper.navigatePlaylist(root.context, playlist.id, true)
            }

            root.setOnLongClickListener {
                val playlistOptionsDialog = PlaylistOptionsBottomSheet(
                    playlistId = playlist.id!!,
                    playlistName = playlist.name!!,
                    isOwner = true
                )
                playlistOptionsDialog.show(
                    (root.context as BaseActivity).supportFragmentManager,
                    PlaylistOptionsBottomSheet::class.java.name
                )
                true
            }
        }
    }
}
