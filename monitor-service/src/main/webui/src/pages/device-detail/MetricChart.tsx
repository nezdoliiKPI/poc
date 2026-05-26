import { useMemo } from 'react';
import {
  AreaChart, Area, XAxis, YAxis,
  Tooltip, CartesianGrid, ResponsiveContainer,
} from 'recharts';
import { COLORS, CHART, type MetricDef } from '../../theme';
import type { AnyPoint } from '../../types';
import {
  fmtFullDateTime, makeTickFormatter, generateTimeTicks,
  formatMetricValue, toPlotPoints, prepareChartData,
} from './helpers';

/**
 * Recharts area chart for a single telemetry metric.
 * Handles tick generation, gradient fill, tooltip formatting, and empty state.
 */
export function MetricChart({
  data,
  metric,
  windowMinutes,
  fromMs,
  toMs,
}: {
  data: AnyPoint[];
  metric: MetricDef;
  windowMinutes: number;
  fromMs: number;
  toMs: number;
}) {
  const gradId  = `grad-${metric.key}`;
  const tickFmt = useMemo(() => makeTickFormatter(windowMinutes), [windowMinutes]);
  const ticks   = useMemo(
    () => generateTimeTicks(fromMs, toMs, windowMinutes),
    [fromMs, toMs, windowMinutes],
  );

  const plotData = useMemo(
    () => prepareChartData(toPlotPoints(data), metric.key, windowMinutes),
    [data, metric.key, windowMinutes],
  );

  const isEmpty = plotData.length === 0;

  return (
    <div style={{
      background: COLORS.bgCard,
      border: '1px solid ' + COLORS.border,
      borderRadius: 8,
      padding: 16,
      // Overflow visible so the recharts tooltip is never clipped by this container.
      overflow: 'visible',
      position: 'relative',
    }}>
      <h3 style={{ margin: '0 0 12px', fontSize: 13, fontWeight: 500, color: COLORS.textPrimary }}>
        {metric.label}
        {metric.unit && (
          <span style={{ marginLeft: 4, fontWeight: 400, color: COLORS.textMuted }}>
            ({metric.unit})
          </span>
        )}
      </h3>

      {isEmpty ? (
        <div style={{
          height: 200,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          fontSize: 13,
          color: COLORS.textMuted,
          border: `1px dashed ${COLORS.border}`,
          borderRadius: 6,
        }}>
          Немає даних за вибраний період
        </div>
      ) : (
        <ResponsiveContainer width="100%" height={200}>
          <AreaChart data={plotData} margin={{ top: 4, right: 8, left: -8, bottom: 0 }}>
            <defs>
              <linearGradient id={gradId} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%"  stopColor={metric.color} stopOpacity={0.20} />
                <stop offset="95%" stopColor={metric.color} stopOpacity={0}    />
              </linearGradient>
            </defs>

            <CartesianGrid strokeDasharray="3 3" stroke={CHART.grid} />

            <XAxis
              dataKey="_t"
              type="number"
              scale="time"
              domain={[fromMs, toMs]}
              ticks={ticks}
              tickFormatter={tickFmt}
              stroke={CHART.axis}
              tick={{ fontSize: 10, fill: CHART.axis }}
              tickLine={false}
              minTickGap={36}
              allowDataOverflow
            />
            <YAxis
              stroke={CHART.axis}
              tick={{ fontSize: 10, fill: CHART.axis }}
              tickLine={false}
              axisLine={false}
              width={40}
            />

            {/* wrapperStyle zIndex ensures the tooltip floats above all sibling cards. */}
            <Tooltip
              contentStyle={CHART.tooltip}
              labelStyle={{ color: CHART.tooltip.color, marginBottom: 4 }}
              itemStyle={{ color: CHART.tooltip.color }}
              labelFormatter={(t: number) => fmtFullDateTime(t)}
              formatter={(val: number) => [formatMetricValue(val, metric), metric.label]}
              cursor={{ stroke: COLORS.borderStrong, strokeWidth: 1, strokeDasharray: '4 2' }}
              wrapperStyle={{ zIndex: 100, pointerEvents: 'none' }}
            />

            <Area
              type="monotone"
              dataKey={metric.key}
              stroke={metric.color}
              strokeWidth={1.5}
              fill={`url(#${gradId})`}
              // activeDot renders a visible circle on the hovered data point.
              activeDot={{ r: 4, fill: metric.color, stroke: COLORS.bgCard, strokeWidth: 2 }}
              dot={false}
              isAnimationActive={false}
            />
          </AreaChart>
        </ResponsiveContainer>
      )}
    </div>
  );
}
