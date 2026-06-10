ALTER TABLE res_material
  ADD COLUMN IF NOT EXISTS rejected_reason TEXT;

COMMENT ON COLUMN res_material.rejected_reason IS 'Material rejection reason';
