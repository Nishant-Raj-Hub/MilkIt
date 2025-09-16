/**
 * Data export utilities for milk records
 */

const MilkRecord = require("../models/MilkRecord")

/**
 * Generate CSV data for milk records
 */
const generateCSV = (records) => {
  if (!records || records.length === 0) {
    return "No records found"
  }

  const headers = ["Date", "Liters", "Status", "Type", "Notes", "Auto Marked", "Created At"]
  const csvRows = [headers.join(",")]

  records.forEach((record) => {
    const row = [
      record.date.toISOString().split("T")[0], // Date only
      record.liters,
      record.status,
      record.milkType,
      `"${record.notes || ""}"`, // Wrap in quotes for CSV
      record.isAutoMarked ? "Yes" : "No",
      record.createdAt.toISOString(),
    ]
    csvRows.push(row.join(","))
  })

  return csvRows.join("\n")
}

/**
 * Generate text summary for milk records
 */
const generateTextSummary = (records, startDate, endDate) => {
  if (!records || records.length === 0) {
    return "No milk records found for the specified period."
  }

  const totalLiters = records.reduce((sum, record) => sum + record.liters, 0)
  const averageLiters = totalLiters / records.length
  const receivedCount = records.filter((r) => r.status === "received").length
  const missedCount = records.filter((r) => r.status === "not_received").length
  const partialCount = records.filter((r) => r.status === "partial").length

  let summary = `MilkIt - Milk Delivery Summary\n`
  summary += `=====================================\n\n`

  if (startDate && endDate) {
    summary += `Period: ${startDate} to ${endDate}\n`
  }

  summary += `Total Records: ${records.length}\n`
  summary += `Total Milk Received: ${totalLiters.toFixed(2)} liters\n`
  summary += `Average per Day: ${averageLiters.toFixed(2)} liters\n\n`

  summary += `Delivery Status:\n`
  summary += `- Received: ${receivedCount} days\n`
  summary += `- Missed: ${missedCount} days\n`
  summary += `- Partial: ${partialCount} days\n\n`

  summary += `Daily Records:\n`
  summary += `==============\n`

  records
    .sort((a, b) => new Date(a.date) - new Date(b.date))
    .forEach((record) => {
      const date = record.date.toISOString().split("T")[0]
      const status = record.status.charAt(0).toUpperCase() + record.status.slice(1)
      const autoMark = record.isAutoMarked ? " (Auto)" : ""
      summary += `${date}: ${record.liters}L - ${status}${autoMark}\n`
      if (record.notes) {
        summary += `  Note: ${record.notes}\n`
      }
    })

  return summary
}

/**
 * Get export data for a user within date range
 */
const getExportData = async (userId, startDate, endDate, format = "text") => {
  try {
    const query = { userId }

    if (startDate || endDate) {
      query.date = {}
      if (startDate) {
        const start = new Date(startDate)
        start.setHours(0, 0, 0, 0)
        query.date.$gte = start
      }
      if (endDate) {
        const end = new Date(endDate)
        end.setHours(23, 59, 59, 999)
        query.date.$lte = end
      }
    }

    const records = await MilkRecord.find(query).sort({ date: 1 }).lean()

    if (format === "csv") {
      return {
        data: generateCSV(records),
        filename: `milkit-records-${startDate || "all"}-${endDate || "all"}.csv`,
        contentType: "text/csv",
      }
    }

    return {
      data: generateTextSummary(records, startDate, endDate),
      filename: `milkit-summary-${startDate || "all"}-${endDate || "all"}.txt`,
      contentType: "text/plain",
    }
  } catch (error) {
    console.error("Export data error:", error)
    throw error
  }
}

module.exports = {
  generateCSV,
  generateTextSummary,
  getExportData,
}
