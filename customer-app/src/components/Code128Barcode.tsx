import JsBarcode from 'jsbarcode';
import { useMemo } from 'react';
import Svg, { Rect } from 'react-native-svg';

type Props = {
  value: string;
  height?: number;
  color?: string;
  backgroundColor?: string;
};

type JsBarcodeEncoding = {
  data: string;
};

type JsBarcodeResult = {
  encodings: JsBarcodeEncoding[];
};

const QUIET_ZONE_UNITS = 8;

function encodeCode128(value: string) {
  if (!value) {
    return '';
  }

  const result: JsBarcodeResult = { encodings: [] };

  JsBarcode(result as never, value, {
    format: 'CODE128',
    displayValue: false,
    margin: 0,
    width: 1,
    height: 1,
  });

  return result.encodings.map((encoding) => encoding.data).join('');
}

function buildBars(binaryPattern: string) {
  const bars: Array<{ x: number; width: number }> = [];
  let x = QUIET_ZONE_UNITS;
  let runStart = -1;

  for (let index = 0; index < binaryPattern.length; index += 1) {
    const bit = binaryPattern[index];
    if (bit === '1' && runStart === -1) {
      runStart = index;
    }
    if ((bit !== '1' || index === binaryPattern.length - 1) && runStart !== -1) {
      const runEnd = bit === '1' && index === binaryPattern.length - 1 ? index + 1 : index;
      bars.push({
        x: x + runStart,
        width: runEnd - runStart,
      });
      runStart = -1;
    }
  }

  return bars;
}

export function Code128Barcode({
  value,
  height = 156,
  color = '#020617',
  backgroundColor = '#ffffff',
}: Props) {
  const encoded = useMemo(() => {
    try {
      return encodeCode128(value);
    } catch {
      return '';
    }
  }, [value]);

  const bars = useMemo(() => buildBars(encoded), [encoded]);
  const totalUnits = QUIET_ZONE_UNITS * 2 + encoded.length;

  if (!value || !encoded || bars.length === 0) {
    return null;
  }

  return (
    <Svg
      width="100%"
      height={height}
      viewBox={`0 0 ${totalUnits} ${height}`}
      preserveAspectRatio="none"
    >
      <Rect x={0} y={0} width={totalUnits} height={height} fill={backgroundColor} />
      {bars.map((bar) => (
        <Rect
          key={`${bar.x}-${bar.width}`}
          x={bar.x}
          y={0}
          width={bar.width}
          height={height}
          fill={color}
        />
      ))}
    </Svg>
  );
}
