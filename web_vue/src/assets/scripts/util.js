export const nWise = (n, array) => {
  const iterators = Array(n).fill().map(() => array[Symbol.iterator]());
  iterators.forEach((it, index) => Array(index).fill().forEach(() => it.next()));
  return Array(array.length - n + 1).fill().map(() => (iterators.map(it => it.next().value)));
};

export const pairWise = (array) => nWise(2, array);

export const sum = (arr) => arr.reduce((a,b) => a + b);

export const range = (n) => [...Array(n).keys()];

export const rand = (min, max) => Math.random() * (max - min) + min;

Array.prototype.last = function() { return this[this.length - 1]; };

export const flatten = (array) => array.reduce((flat, toFlatten) => (flat.concat(Array.isArray(toFlatten) ? flatten(toFlatten) : toFlatten)), []);
