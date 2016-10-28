package com.lahodiuk.sampling.gibbs;

import java.util.AbstractList;
import java.util.Collection;

public class PersistentTreeList<T> extends AbstractList<T> {

	private final PersistentTree<T> tree;

	private PersistentTreeList(PersistentTree<T> tree) {
		this.tree = tree;
	}

	public PersistentTreeList(Collection<T> items) {
		this.tree = new PersistentTree<>(items);
	}

	@Override
	public T get(int index) {
		return this.tree.get(index);
	}

	public PersistentTreeList<T> updateItem(int index, T newItem) {
		PersistentTree<T> newTree = this.tree.update(index, newItem);
		if (newTree == this.tree) {
			return this;
		}
		return new PersistentTreeList<>(newTree);
	}

	@Override
	public int size() {
		return this.tree.getLeafsCount();
	}

	public static class PersistentTree<T> {

		private final Node<T> root;
		private final int lastItemIdx;

		private PersistentTree(Node<T> root, int lastItemIdx) {
			this.root = root;
			this.lastItemIdx = lastItemIdx;
		}

		public PersistentTree(Collection<T> items) {
			@SuppressWarnings("unchecked")
			T[] itemsArr = (T[]) items.toArray();
			this.root = this.buildTree(itemsArr, 0, itemsArr.length - 1);
			this.lastItemIdx = itemsArr.length - 1;
		}

		public T get(int idx) {
			return this.get(this.root, idx, 0, this.lastItemIdx);
		}

		public PersistentTree<T> update(int idx, T newItem) {
			Node<T> newRoot = this.update(this.root, idx, newItem, 0, this.lastItemIdx);
			if (newRoot == this.root) {
				return this;
			}
			return new PersistentTree<>(newRoot, this.lastItemIdx);
		}

		private Node<T> update(Node<T> node, int idx, T newItem, int left, int right) {
			if (left == right) {
				if (newItem.equals(node.item)) {
					return node;
				} else {
					return new Node<T>(null, null, newItem);
				}
			} else {
				Node<T> leftNode;
				Node<T> rightNode;
				int mid = (left + right) / 2;
				if (idx > mid) {
					leftNode = node.left;
					rightNode = this.update(node.right, idx, newItem, mid + 1, right);
				} else {
					leftNode = this.update(node.left, idx, newItem, left, mid);
					rightNode = node.right;
				}
				if ((leftNode == node.left) && (rightNode == node.right)) {
					return node;
				} else {
					return new Node<>(leftNode, rightNode, null);
				}
			}
		}

		public int getLeafsCount() {
			return this.lastItemIdx + 1;
		}

		private T get(Node<T> node, int idx, int left, int right) {
			while (left != right) {
				int mid = (left + right) / 2;
				if (idx > mid) {
					node = node.right;
					left = mid + 1;
				} else {
					node = node.left;
					right = mid;
				}
			}
			return node.item;
		}

		private Node<T> buildTree(T[] items, int l, int r) {
			if (l == r) {
				return new Node<>(null, null, items[l]);
			} else {
				int mid = (l + r) / 2;
				Node<T> left = this.buildTree(items, l, mid);
				Node<T> right = this.buildTree(items, mid + 1, r);
				return new Node<>(left, right, null);
			}
		}

		public static class Node<T> {
			public final Node<T> left;
			public final Node<T> right;
			public final T item;

			public Node(Node<T> left, Node<T> right, T item) {
				this.left = left;
				this.right = right;
				this.item = item;
			}
		}
	}
}